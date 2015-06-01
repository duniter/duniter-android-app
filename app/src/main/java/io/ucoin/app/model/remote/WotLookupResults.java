package io.ucoin.app.model.remote;

import java.util.List;

public class WotLookupResults {

    private boolean partial;
    
    private List<WotLookupResult> results;

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public List<WotLookupResult> getResults() {
        return results;
    }

    public void setResults(List<WotLookupResult> results) {
        this.results = results;
    }
}
