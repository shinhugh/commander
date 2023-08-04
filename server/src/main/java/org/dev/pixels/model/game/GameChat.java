package org.dev.pixels.model.game;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameChat {
    private long srcPlayerId;
    private Long dstPlayerId;
    private Boolean toPublic;
    private String content;

    public long getSrcPlayerId() {
        return srcPlayerId;
    }

    public void setSrcPlayerId(long srcPlayerId) {
        this.srcPlayerId = srcPlayerId;
    }

    public Long getDstPlayerId() {
        return dstPlayerId;
    }

    public void setDstPlayerId(Long dstPlayerId) {
        this.dstPlayerId = dstPlayerId;
    }

    public Boolean getToPublic() {
        return toPublic;
    }

    public void setToPublic(Boolean toPublic) {
        this.toPublic = toPublic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
