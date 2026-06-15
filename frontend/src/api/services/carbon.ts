import { get } from '../config/http';
import { API_PATHS } from '../config/paths';

export interface CarbonStats {
  monthlySavingKg: number;
  totalSavingKg: number;
  treeEquivalent: number;
  transactionCount: number;
  participantCount: number;
}

const carbon = {
  getStats: () => get<CarbonStats>(API_PATHS.CARBON.STATS),
};

export default carbon;
