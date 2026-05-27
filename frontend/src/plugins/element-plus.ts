import type { App } from 'vue';
import {
  ElButton, ElInput, ElForm, ElFormItem, ElTable, ElTableColumn,
  ElMenu, ElMenuItem, ElSubMenu, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElAvatar, ElIcon, ElMessage, ElMessageBox, ElDialog, ElSelect, ElOption,
  ElUpload, ElTag, ElBadge, ElPagination, ElTabs, ElTabPane, ElSteps, ElStep,
  ElCard, ElSwitch, ElRadio, ElRadioGroup, ElCheckbox, ElCheckboxGroup,
  ElDatePicker, ElInputNumber, ElTooltip, ElPopover, ElProgress,
  ElAlert, ElLoading, ElNotification, ElBreadcrumb, ElBreadcrumbItem,
  ElDescriptions, ElDescriptionsItem, ElEmpty, ElSkeleton, ElResult,
  ElDivider, ElLink, ElImage, ElCarousel, ElCarouselItem, ElCollapse,
  ElCollapseItem, ElTimeline, ElTimelineItem, ElBacktop, ElAffix,
  ElScrollbar, ElDrawer, ElTree, ElColorPicker, ElSlider, ElRate,
  ElPageHeader, ElCascader, ElTransfer, ElInfiniteScroll,
  ElSpace, ElContainer, ElHeader, ElAside, ElMain, ElFooter,
  ElRow, ElCol, ElButtonGroup, ElText, ElStatistic, ElCountdown,
  ElTour, ElTourStep, ElSegmented, ElCollapseTransition,
} from 'element-plus';

const components = [
  ElButton, ElInput, ElForm, ElFormItem, ElTable, ElTableColumn,
  ElMenu, ElMenuItem, ElSubMenu, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElAvatar, ElIcon, ElDialog, ElSelect, ElOption, ElUpload, ElTag, ElBadge,
  ElPagination, ElTabs, ElTabPane, ElSteps, ElStep, ElCard, ElSwitch,
  ElRadio, ElRadioGroup, ElCheckbox, ElCheckboxGroup, ElDatePicker,
  ElInputNumber, ElTooltip, ElPopover, ElProgress, ElAlert, ElBreadcrumb,
  ElBreadcrumbItem, ElDescriptions, ElDescriptionsItem, ElEmpty, ElSkeleton,
  ElResult, ElDivider, ElLink, ElImage, ElCarousel, ElCarouselItem,
  ElCollapse, ElCollapseItem, ElTimeline, ElTimelineItem, ElBacktop,
  ElAffix, ElScrollbar, ElDrawer, ElTree, ElColorPicker, ElSlider, ElRate,
  ElPageHeader, ElCascader, ElTransfer, ElSpace, ElContainer, ElHeader,
  ElAside, ElMain, ElFooter, ElRow, ElCol, ElButtonGroup, ElText,
  ElStatistic, ElCountdown, ElTour, ElTourStep, ElSegmented, ElCollapseTransition,
];

const plugins = [
  ElLoading,
];

export function setupElementPlus(app: App) {
  components.forEach(component => app.component(component.name!, component));
  plugins.forEach(plugin => app.use(plugin));
  app.config.globalProperties.$message = ElMessage;
  app.config.globalProperties.$msgbox = ElMessageBox;
  app.config.globalProperties.$alert = ElMessageBox.alert;
  app.config.globalProperties.$confirm = ElMessageBox.confirm;
  app.config.globalProperties.$prompt = ElMessageBox.prompt;
  app.config.globalProperties.$notify = ElNotification;
}
