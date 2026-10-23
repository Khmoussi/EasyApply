package com.example.EasyApply.Data;

public record EmailAttachment(byte[] content, String filename) {

    public static EmailAttachment empty() {
        return new EmailAttachment(null, null);
    }

    public boolean isPresent() {
        return content != null && content.length > 0 && filename != null && !filename.isBlank();
    }
}
