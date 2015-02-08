package io.ucoin.app.service.remote;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.Peer;
import io.ucoin.app.model.TxOutput;
import io.ucoin.app.model.TxSource;
import io.ucoin.app.model.TxSourceResults;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.InsufficientCreditException;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.DigestUtils;

public class TransactionRemoteService extends BaseRemoteService {

    private static final String TAG = "TransactionService";

    public static final String URL_TX_BASE = "/tx";

    public static final String URL_TX_PROCESS = URL_TX_BASE + "/process";

    public static final String URL_TX_SOURCES = URL_TX_BASE + "/sources/%s";


	private CryptoService cryptoService;

	public TransactionRemoteService() {
		super();
	}

	@Override
	public void initialize() {
        super.initialize();
        cryptoService = ServiceLocator.instance().getCryptoService();
	}

	public String transfert(Wallet wallet, String destPubKey, long amount,
			String comments) throws InsufficientCreditException {
		
		// http post /tx/process
		HttpPost httpPost = new HttpPost(
				getPath(URL_TX_PROCESS));

		// compute transaction
		String transaction = getTransaction(wallet, destPubKey, amount,
                comments, false);

        Log.d(TAG, String.format(
                "Will send transaction document: \n------\n%s------",
                transaction));

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("transaction", transaction));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
		} catch (UnsupportedEncodingException e) {
			throw new UCoinTechnicalException(e);
		}

		String selfResult = executeRequest(httpPost, String.class);
		Log.i(TAG, "received from /tx/process: " + selfResult);


        String fingerprint = DigestUtils.sha1Hex(transaction);
        Log.d(TAG, String.format(
                "Fingerprint: %s",
                fingerprint));
        return fingerprint;
	}

	public TxSourceResults getSources(String pubKey) {
		Log.d(TAG, String.format("Get sources by pubKey [%s]", pubKey));

		// get parameter
		String path = String.format(URL_TX_SOURCES, pubKey);
		TxSourceResults result = executeRequest(path, TxSourceResults.class);

		// Compute the balance
		result.setBalance(computeBalance(result.getSources()));

		return result;
	}

    public Long getCredit(Peer peer, String pubKey) {
        Log.d(TAG, String.format("Get credit by pubKey [%s] from [%s]", pubKey, peer.getUrl()));

        // get parameter
        String path = String.format(URL_TX_SOURCES, pubKey);
        TxSourceResults result = executeRequest(peer, path, TxSourceResults.class);

        if (result == null) {
            return null;
        }

        // Compute the balance
        return computeBalance(result.getSources());
    }

	/* -- internal methods -- */

	public String getTransaction(Wallet wallet, String destPubKey,
			long amount, String comments, boolean compact) throws InsufficientCreditException {

		// Retrieve the wallet sources
		TxSourceResults sourceResults = getSources(wallet.getPubKeyHash());
		if (sourceResults == null) {
			throw new UCoinTechnicalException("Unable to load user sources.");
		}

		List<TxSource> sources = sourceResults.getSources();
		if (sources == null || sources.isEmpty()) {
			throw new InsufficientCreditException(
					"Insufficient credit : no credit found.");
		}

		List<TxSource> txInputs = new ArrayList<TxSource>();
		List<TxOutput> txOutputs = new ArrayList<TxOutput>();
		computeTransactionInputsAndOuputs(wallet.getPubKeyHash(), destPubKey,
				sources, amount, txInputs, txOutputs);

		String transaction = getTransaction(wallet.getCurrency(),
				wallet.getPubKeyHash(), destPubKey, txInputs, txOutputs,
				comments);

		String signature = cryptoService.sign(transaction, wallet.getSecKey());

		if (compact) {
			transaction = getCompactTransaction(wallet.getCurrency(),
					wallet.getPubKeyHash(), destPubKey, txInputs, txOutputs,
					comments);
		}

		return new StringBuilder().append(transaction).append(signature)
				.append('\n').toString();
	}

	public String getTransaction(String currency, String srcPubKey,
			String destPubKey, List<TxSource> inputs, List<TxOutput> outputs,
			String comments) {

		StringBuilder sb = new StringBuilder();
		sb.append("Version: 1\n").append("Type: Transaction\n")
				.append("Currency: ").append(currency).append('\n')
				.append("Issuers:\n")
				// add issuer pubkey
				.append(srcPubKey).append('\n');

		// Inputs coins
		sb.append("Inputs:\n");
		for (TxSource input : inputs) {
			// INDEX:SOURCE:NUMBER:FINGERPRINT:AMOUNT
			sb.append(0).append(':').append(input.getType()).append(':')
					.append(input.getNumber()).append(':')
					.append(input.getFingerprint()).append(':')
					.append(input.getAmount()).append('\n');
		}

		// Output
		sb.append("Outputs:\n");
		for (TxOutput output : outputs) {
			// PUBLIC_KEY:AMOUNT
			sb.append(output.getPubKey()).append(':')
					.append(output.getAmount()).append('\n');
		}

		// Comment
		sb.append("Comment: ").append(comments).append('\n');

		return sb.toString();
	}

	public String getCompactTransaction(String currency, String srcPubKey,
			String destPubKey, List<TxSource> inputs, List<TxOutput> outputs,
			String comments) {

		boolean hasComment = comments != null && comments.length() > 0;
		StringBuilder sb = new StringBuilder();
		sb.append("TX:")
				// VERSION
				.append(PROTOCOL_VERSION).append(':')
				// NB_ISSUERS
				.append("1:")
				// NB_INPUTS
				.append(inputs.size()).append(':')
				// NB_OUTPUTS
				.append(outputs.size()).append(':')
				// HAS_COMMENT
				.append(hasComment ? 1 : 0).append('\n')
				// issuer pubkey
				.append(srcPubKey).append('\n');

		// Inputs coins
		for (TxSource input : inputs) {
			// INDEX:SOURCE:NUMBER:FINGERPRINT:AMOUNT
			sb.append(0).append(':').append(input.getType()).append(':')
					.append(input.getNumber()).append(':')
					.append(input.getFingerprint()).append(':')
					.append(input.getAmount()).append('\n');
		}

		// Output
		for (TxOutput output : outputs) {
			// PUBLIC_KEY:AMOUNT
			sb.append(output.getPubKey()).append(':')
					.append(output.getAmount()).append('\n');
		}

		// Comment
		if (hasComment) {
		sb.append(comments).append('\n');
		}
		return sb.toString();
	}

	public void computeTransactionInputsAndOuputs(String srcPubKey,
			String destPubKey, List<TxSource> sources, long amount,
			List<TxSource> inputs, List<TxOutput> outputs) throws InsufficientCreditException{

		long rest = amount;
		long restForHimSelf = 0;

		for (TxSource source : sources) {
			long srcAmount = source.getAmount();
			inputs.add(source);
			if (srcAmount >= rest) {
				restForHimSelf = srcAmount - rest;
				rest = 0;
				break;
			}
			rest -= srcAmount;
		}

		if (rest > 0) {
			throw new InsufficientCreditException(String.format(
					"Insufficient credit. Need %s more units.", rest));
		}

		// outputs
		{
			TxOutput output = new TxOutput();
			output.setPubKey(destPubKey);
			output.setAmount(amount);
			outputs.add(output);
		}
		if (restForHimSelf > 0) {
			TxOutput output = new TxOutput();
			output.setPubKey(srcPubKey);
			output.setAmount(restForHimSelf);
			outputs.add(output);
		}
	}

	protected long computeBalance(List<TxSource> sources) {
		if (sources == null) {
			return 0;
		}

		long balance = 0;
		for (TxSource source : sources) {
			balance += source.getAmount();
		}
		return balance;
	}
}
