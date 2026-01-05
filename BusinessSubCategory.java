
package com.common;

import java.util.Arrays;
import java.util.List;

public class BusinessSubCategory extends NewsSubCategory {

    private final BusinessType businessType;

    public BusinessSubCategory(BusinessType businessType) {
        this.businessType = businessType;
    }

    @Override
    public String getDisplayName() {
        return businessType.getDisplayName();
    }
    
    @Override
    public String getEmoji() {
        return businessType.getEmoji();
    }

    @Override
    public List<String> getKeywords() {
        return businessType.getKeywords();
    }

    public enum BusinessType {
        MARKETS("Markets", "📈", Arrays.asList("stock market", "dow jones", "nasdaq", "investing", "shares")),
        ECONOMY("Economy", "🌍", Arrays.asList("economy", "inflation", "gdp", "interest rates", "federal reserve")),
        CORPORATE("Corporate News", "🏢", Arrays.asList("corporate", "earnings", "ceo", "merger", "acquisition")),
        FINANCE("Personal Finance", "💰", Arrays.asList("personal finance", "savings", "retirement", "investment advice")),
        REAL_ESTATE("Real Estate", "🏠", Arrays.asList("real estate", "housing market", "mortgage rates", "property"));

        private final String displayName;
        private final String emoji;
        private final List<String> keywords;

        BusinessType(String displayName, String emoji, List<String> keywords) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.keywords = keywords;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public List<String> getKeywords() { return keywords; }
    }
}
