package io.ucoin.app.service.remote;

import android.util.Log;

import java.util.List;

import io.ucoin.app.model.remote.UdHistoryMovement;
import io.ucoin.app.model.remote.UdHistoryResults;
import io.ucoin.app.technical.ObjectUtils;

public class UdRemoteService extends BaseRemoteService {

    private static final String TAG = "UdRemoteService";

    public static final String URL_UD_BASE = "/ud";

    public static final String URL_UD_HISTORY = URL_UD_BASE + "/history/%s";


	public UdRemoteService() {
		super();
	}

	@Override
	public void initialize() {
        super.initialize();
	}


    public List<UdHistoryMovement> getUdHistory(long currencyId, String pubKey) {
        ObjectUtils.checkNotNull(pubKey);

        Log.d(TAG, String.format("Get UD history by pubKey [%s]", pubKey));

        // get parameter
        String path = String.format(URL_UD_HISTORY, pubKey);
        UdHistoryResults result = executeRequest(currencyId, path, UdHistoryResults.class);

        if (result == null
                || result.getHistory() == null) {
            return null;
        }

        return result.getHistory().getHistory();
    }

	/* -- internal methods -- */

}
