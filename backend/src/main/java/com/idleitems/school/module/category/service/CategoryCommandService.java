package com.idleitems.school.module.category.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.CategoryChangedEvent;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.entity.CategoryChangeLog;
import com.idleitems.school.module.category.repository.CategoryChangeLogRepository;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryChangeLogRepository categoryChangeLogRepository;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Category createCategory(Category category, Long operatorId) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Category name required");
        }

        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Parent not found"));
            if (parent.getLevel() >= 3) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Max 3 levels");
            }
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        List<Category> siblings = category.getParentId() != null
                ? categoryRepository.findByParentId(category.getParentId())
                : categoryRepository.findByParentIdIsNull();
        boolean nameExists = siblings.stream()
                .anyMatch(c -> c.getName().equals(category.getName()));
        if (nameExists) {
            throw new BusinessException(ErrorCode.CONFLICT, "Name already exists");
        }

        if (category.getSort() == null) category.setSort(0);
        if (category.getStatus() == null) category.setStatus(true);

        Category saved = categoryRepository.save(category);
        recordChangeLog(saved, CategoryChangeLog.ActionType.CREATE, operatorId, null);
        normalizeSortValues(category.getParentId());
        clearCategoryCache();
        publishCategoryChanged(saved.getId(), "CREATE");
        return saved;
    }

    @Transactional
    public Category updateCategory(Long id, Category updateData, Long operatorId) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found"));

        Map<String, Object> changes = new HashMap<>();

        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            List<Category> siblings = existing.getParentId() != null
                    ? categoryRepository.findByParentId(existing.getParentId())
                    : categoryRepository.findByParentIdIsNull();
            boolean nameExists = siblings.stream()
                    .anyMatch(c -> !c.getId().equals(id) && c.getName().equals(updateData.getName()));
            if (nameExists) {
                throw new BusinessException(ErrorCode.CONFLICT, "Name already exists");
            }
            changes.put("name", Map.of("old", existing.getName(), "new", updateData.getName()));
            existing.setName(updateData.getName());
        }

        if (updateData.getDescription() != null) {
            changes.put("description", Map.of("old", existing.getDescription() != null ? existing.getDescription() : "", "new", updateData.getDescription()));
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getIcon() != null) {
            changes.put("icon", Map.of("old", existing.getIcon() != null ? existing.getIcon() : "", "new", updateData.getIcon()));
            existing.setIcon(updateData.getIcon());
        }
        if (updateData.getSort() != null) {
            changes.put("sort", Map.of("old", existing.getSort(), "new", updateData.getSort()));
            existing.setSort(updateData.getSort());
        }
        if (updateData.getKeywords() != null) {
            changes.put("keywords", Map.of("old", existing.getKeywords() != null ? existing.getKeywords() : "", "new", updateData.getKeywords()));
            existing.setKeywords(updateData.getKeywords());
        }

        if (updateData.getParentId() != null && !updateData.getParentId().equals(existing.getParentId())) {
            if (updateData.getParentId().equals(id)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot set self as parent");
            }
            Category newParent = categoryRepository.findById(updateData.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Parent not found"));
            if (newParent.getLevel() >= 3) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Max 3 levels");
            }
            changes.put("parentId", Map.of("old", existing.getParentId() != null ? existing.getParentId() : "", "new", updateData.getParentId()));
            existing.setParentId(updateData.getParentId());
            existing.setLevel(newParent.getLevel() + 1);
        }

        existing.setUpdatedBy(operatorId);
        Category saved = categoryRepository.save(existing);

        if (!changes.isEmpty()) {
            recordChangeLog(saved, CategoryChangeLog.ActionType.UPDATE, operatorId, changes);
        }
        clearCategoryCache();
        publishCategoryChanged(saved.getId(), "UPDATE");
        return saved;
    }

    @Transactional
    public void deleteCategory(Long id, Long operatorId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found"));

        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Has subcategories");
        }

        Long directItemCount = itemRepository.countByCategoryId(id);
        if (directItemCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Has items");
        }

        recordChangeLog(category, CategoryChangeLog.ActionType.DELETE, operatorId, null);
        categoryRepository.deleteById(id);
        normalizeSortValues(category.getParentId());
        clearCategoryCache();
        publishCategoryChanged(id, "DELETE");
    }

    @Transactional
    public void batchDeleteCategories(List<Long> ids, Long operatorId) {
        List<String> errors = new ArrayList<>();
        for (Long id : ids) {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                errors.add("ID " + id + " not found");
                continue;
            }
            List<Category> children = categoryRepository.findByParentId(id);
            if (!children.isEmpty()) {
                errors.add("Category '" + category.getName() + "' has subcategories");
                continue;
            }
            Long itemCount = itemRepository.countByCategoryId(id);
            if (itemCount > 0) {
                errors.add("Category '" + category.getName() + "' has items");
                continue;
            }
            recordChangeLog(category, CategoryChangeLog.ActionType.DELETE, operatorId, null);
            categoryRepository.deleteById(id);
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Cannot delete: " + String.join("; ", errors));
        }
        clearCategoryCache();
        publishCategoryChanged(null, "BATCH_DELETE");
    }

    @Transactional
    public Category toggleCategoryStatus(Long id, Boolean status, Long operatorId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found"));

        Map<String, Object> changes = Map.of("status", Map.of("old", category.getStatus(), "new", status));
        category.setStatus(status);
        category.setUpdatedBy(operatorId);
        Category saved = categoryRepository.save(category);

        recordChangeLog(saved, CategoryChangeLog.ActionType.STATUS_CHANGE, operatorId, changes);
        clearCategoryCache();
        publishCategoryChanged(id, "STATUS_CHANGE");
        return saved;
    }

    @Transactional
    public Map<String, Object> importCategories(MultipartFile file, Long operatorId) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Empty file");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 2) {
                        errors.add("Line " + lineNum + ": insufficient data");
                        failCount++;
                        continue;
                    }

                    Category category = new Category();
                    category.setName(parts[1].trim());
                    if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                        String parentIdStr = parts[2].trim();
                        if (!"无".equals(parentIdStr)) {
                            category.setParentId(Long.parseLong(parentIdStr));
                        }
                    }
                    if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                        String levelStr = parts[3].trim();
                        Integer level = parseLevel(levelStr);
                        if (level != null) {
                            category.setLevel(level);
                        }
                    }
                    if (parts.length > 4 && !parts[4].trim().isEmpty()) category.setSort(Integer.parseInt(parts[4].trim()));
                    if (parts.length > 5) {
                        String statusStr = parts[5].trim();
                        boolean status = "ACTIVE".equalsIgnoreCase(statusStr) || "启用".equals(statusStr);
                        category.setStatus(status);
                    }
                    if (parts.length > 6) category.setIcon(parts[6].trim());
                    if (parts.length > 7) category.setDescription(parts[7].trim());
                    if (parts.length > 8) category.setKeywords(parts[8].trim());

                    if (category.getLevel() == null) category.setLevel(category.getParentId() != null ? 2 : 1);
                    if (category.getSort() == null) category.setSort(0);
                    if (category.getStatus() == null) category.setStatus(true);

                    categoryRepository.save(category);
                    recordChangeLog(category, CategoryChangeLog.ActionType.CREATE, operatorId, null);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                    failCount++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Read file failed: " + e.getMessage());
        }

        clearCategoryCache();
        if (successCount > 0) {
            publishCategoryChanged(null, "IMPORT");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    /**
     * 解析中文层级描述为数字
     * @param levelStr 层级字符串，支持数字或中文描述
     * @return 对应的层级数字，解析失败返回null
     */
    private Integer parseLevel(String levelStr) {
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return null;
        }
        
        // 尝试直接解析数字
        try {
            return Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            // 继续尝试中文描述
        }
        
        // 中文描述映射
        switch (levelStr) {
            case "一级分类":
            case "第1级分类":
                return 1;
            case "二级分类":
            case "第2级分类":
                return 2;
            case "三级分类":
            case "第3级分类":
                return 3;
            default:
                // 尝试解析 "第x级分类" 格式
                if (levelStr.startsWith("第") && levelStr.endsWith("级分类")) {
                    try {
                        String numStr = levelStr.substring(1, levelStr.length() - 3);
                        return Integer.parseInt(numStr);
                    } catch (NumberFormatException ex) {
                        // 解析失败
                    }
                }
                return null;
        }
    }

    public void normalizeSortValues(Long parentId) {
        List<Category> siblings;
        if (parentId != null) {
            siblings = categoryRepository.findByParentId(parentId);
        } else {
            siblings = categoryRepository.findByParentIdIsNull();
        }

        List<Category> mutableSiblings = new ArrayList<>(siblings);
        mutableSiblings.sort(Comparator.comparing(c -> c.getSort() != null ? c.getSort() : 0));

        boolean changed = false;
        for (int i = 0; i < mutableSiblings.size(); i++) {
            Category sibling = mutableSiblings.get(i);
            if (sibling.getSort() == null || sibling.getSort() != i) {
                sibling.setSort(i);
                categoryRepository.save(sibling);
                changed = true;
            }
        }
    }

    private void recordChangeLog(Category category, CategoryChangeLog.ActionType action, Long operatorId, Map<String, Object> changes) {
        try {
            CategoryChangeLog changeLog = new CategoryChangeLog();
            changeLog.setCategoryId(category.getId());
            changeLog.setCategoryName(category.getName());
            changeLog.setAction(action);
            changeLog.setOperatorId(operatorId);
            if (changes != null) {
                changeLog.setDetails(objectMapper.writeValueAsString(changes));
            }
            categoryChangeLogRepository.save(changeLog);
        } catch (Exception e) {
            log.error("Failed to record change log: {}", e.getMessage());
        }
    }

    private void clearCategoryCache() {
        cacheService.delete("categories:all");
        cacheService.delete("categories:tree");
    }

    private void publishCategoryChanged(Long categoryId, String action) {
        try {
            eventPublisher.publishEvent(new CategoryChangedEvent(this, categoryId, action));
        } catch (Exception e) {
            log.error("Failed to publish CategoryChangedEvent: {}", e.getMessage());
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
