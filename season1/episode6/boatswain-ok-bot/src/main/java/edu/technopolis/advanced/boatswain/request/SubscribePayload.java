package edu.technopolis.advanced.boatswain.request;

public class SubscribePayload implements RequestPayload {
    private String url;

    public SubscribePayload() {
    }

    public SubscribePayload(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return "SubscribePayload{" +
                "url='" + url + '\'' +
                '}';
    }
}
