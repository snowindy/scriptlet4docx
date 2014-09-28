package org.scriptlet4docx.docx;

import java.util.List;

public class TemplateContent {
    public static class ContentItem {
        private String identifier;
        private String content;

        public ContentItem(String identifier, String content) {
            super();
            this.identifier = identifier;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getIdentifier() {
            return identifier;
        }

    }

    private List<ContentItem> items;

    public TemplateContent(List<ContentItem> items) {
        
        if (items.size() == 0){
            throw new IllegalStateException("Items size cannot be zero.");
        }
        
        this.items = items;
    }

    public List<ContentItem> getItems() {
        return items;
    }
}
