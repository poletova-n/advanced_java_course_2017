package edu.technopolis.advanced.boatswain.request;

public class SearchRequest extends Request<RequestPayload, SearchRequest> {
   String query;


    public SearchRequest(String searchEndPoint) {
        this.query = searchEndPoint;
    }
    @Override
    protected SearchRequest thiss() {
        return this;
    }

    @Override
    public String getQueryStart() {
        return query;
    }
}
