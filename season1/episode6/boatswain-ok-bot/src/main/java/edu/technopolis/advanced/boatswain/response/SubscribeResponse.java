package edu.technopolis.advanced.boatswain.response;

public class SubscribeResponse extends Response {
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
