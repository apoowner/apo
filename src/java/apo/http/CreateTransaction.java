/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package apo.http;

import apo.Account;
import apo.Appendix;
import apo.Attachment;
import apo.Constants;
import apo.Apo;
import apo.ApoException;
import apo.PhasingParams;
import apo.Transaction;
import apo.crypto.Crypto;
import apo.util.Convert;
import apo.util.JSON;
import apo.util.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.util.Arrays;

import static apo.http.JSONResponses.FEATURE_NOT_AVAILABLE;
import static apo.http.JSONResponses.INCORRECT_DEADLINE;
import static apo.http.JSONResponses.INCORRECT_EC_BLOCK;
import static apo.http.JSONResponses.INCORRECT_LINKED_FULL_HASH;
import static apo.http.JSONResponses.INCORRECT_WHITELIST;
import static apo.http.JSONResponses.MISSING_DEADLINE;
import static apo.http.JSONResponses.MISSING_SECRET_PHRASE;
import static apo.http.JSONResponses.NOT_ENOUGH_FUNDS;

abstract class CreateTransaction extends APIServlet.APIRequestHandler {

    private static final String[] commonParameters = new String[]{"secretPhrase", "publicKey", "feeNQT",
            "deadline", "referencedTransactionFullHash", "broadcast",
            "message", "messageIsText", "messageIsPrunable",
            "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt",
            "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf",
            "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel",
            "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted",
            "phasingLinkedFullHash", "phasingLinkedFullHash", "phasingLinkedFullHash",
            "phasingHashedSecret", "phasingHashedSecretAlgorithm",
            "recipientPublicKey",
            "ecBlockId", "ecBlockHeight"};

    private static String[] addCommonParameters(String[] parameters) {
        String[] result = Arrays.copyOf(parameters, parameters.length + commonParameters.length);
        System.arraycopy(commonParameters, 0, result, parameters.length, commonParameters.length);
        return result;
    }

    CreateTransaction(APITag[] apiTags, String... parameters) {
        super(apiTags, addCommonParameters(parameters));
        if (!getAPITags().contains(APITag.CREATE_TRANSACTION)) {
            throw new RuntimeException("CreateTransaction API " + getClass().getName() + " is missing APITag.CREATE_TRANSACTION tag");
        }
    }

    CreateTransaction(String fileParameter, APITag[] apiTags, String... parameters) {
        super(fileParameter, apiTags, addCommonParameters(parameters));
        if (!getAPITags().contains(APITag.CREATE_TRANSACTION)) {
            throw new RuntimeException("CreateTransaction API " + getClass().getName() + " is missing APITag.CREATE_TRANSACTION tag");
        }
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, Attachment attachment)
            throws ApoException {
        return createTransaction(req, senderAccount, 0, 0, attachment);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId, long amountNQT)
            throws ApoException {
        return createTransaction(req, senderAccount, recipientId, amountNQT, Attachment.ORDINARY_PAYMENT);
    }

    private Appendix.Phasing parsePhasing(HttpServletRequest req) throws ParameterException {
        int finishHeight = ParameterParser.getInt(req, "phasingFinishHeight",
                Apo.getBlockchain().getHeight() + 1,
                Apo.getBlockchain().getHeight() + Constants.MAX_PHASING_DURATION + 1,
                true);
        
        PhasingParams phasingParams = parsePhasingParams(req, "phasing");
        
        byte[][] linkedFullHashes = null;
        String[] linkedFullHashesValues = req.getParameterValues("phasingLinkedFullHash");
        if (linkedFullHashesValues != null && linkedFullHashesValues.length > 0) {
            linkedFullHashes = new byte[linkedFullHashesValues.length][];
            for (int i = 0; i < linkedFullHashes.length; i++) {
                linkedFullHashes[i] = Convert.parseHexString(linkedFullHashesValues[i]);
                if (Convert.emptyToNull(linkedFullHashes[i]) == null || linkedFullHashes[i].length != 32) {
                    throw new ParameterException(INCORRECT_LINKED_FULL_HASH);
                }
            }
        }

        byte[] hashedSecret = Convert.parseHexString(Convert.emptyToNull(req.getParameter("phasingHashedSecret")));
        byte algorithm = ParameterParser.getByte(req, "phasingHashedSecretAlgorithm", (byte) 0, Byte.MAX_VALUE, false);

        return new Appendix.Phasing(finishHeight, phasingParams, linkedFullHashes, hashedSecret, algorithm);
    }

    final PhasingParams parsePhasingParams(HttpServletRequest req, String parameterPrefix) throws ParameterException {
        byte votingModel = ParameterParser.getByte(req, parameterPrefix + "VotingModel", (byte)-1, (byte)5, true);
        long quorum = ParameterParser.getLong(req, parameterPrefix + "Quorum", 0, Long.MAX_VALUE, false);
        long minBalance = ParameterParser.getLong(req, parameterPrefix + "MinBalance", 0, Long.MAX_VALUE, false);
        byte minBalanceModel = ParameterParser.getByte(req, parameterPrefix + "MinBalanceModel", (byte)0, (byte)3, false);
        long holdingId = ParameterParser.getUnsignedLong(req, parameterPrefix + "Holding", false);
        long[] whitelist = null;
        String[] whitelistValues = req.getParameterValues(parameterPrefix + "Whitelisted");
        if (whitelistValues != null && whitelistValues.length > 0) {
            whitelist = new long[whitelistValues.length];
            for (int i = 0; i < whitelistValues.length; i++) {
                whitelist[i] = Convert.parseAccountId(whitelistValues[i]);
                if (whitelist[i] == 0) {
                    throw new ParameterException(INCORRECT_WHITELIST);
                }
            }
        }
        return new PhasingParams(votingModel, holdingId, quorum, minBalance, minBalanceModel, whitelist);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId,
                                            long amountNQT, Attachment attachment) throws ApoException {
        String deadlineValue = req.getParameter("deadline");
        String referencedTransactionFullHash = Convert.emptyToNull(req.getParameter("referencedTransactionFullHash"));
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        String publicKeyValue = Convert.emptyToNull(req.getParameter("publicKey"));
        boolean broadcast = !"false".equalsIgnoreCase(req.getParameter("broadcast")) && secretPhrase != null;
        Appendix.EncryptedMessage encryptedMessage = null;
        Appendix.PrunableEncryptedMessage prunableEncryptedMessage = null;
        if (attachment.getTransactionType().canHaveRecipient() && recipientId != 0) {
            Account recipient = Account.getAccount(recipientId);
            if ("true".equalsIgnoreCase(req.getParameter("encryptedMessageIsPrunable"))) {
                prunableEncryptedMessage = (Appendix.PrunableEncryptedMessage) ParameterParser.getEncryptedMessage(req, recipient, true);
            } else {
                encryptedMessage = (Appendix.EncryptedMessage) ParameterParser.getEncryptedMessage(req, recipient, false);
            }
        }
        Appendix.EncryptToSelfMessage encryptToSelfMessage = ParameterParser.getEncryptToSelfMessage(req);
        Appendix.Message message = null;
        Appendix.PrunablePlainMessage prunablePlainMessage = null;
        if ("true".equalsIgnoreCase(req.getParameter("messageIsPrunable"))) {
            prunablePlainMessage = (Appendix.PrunablePlainMessage) ParameterParser.getPlainMessage(req, true);
        } else {
            message = (Appendix.Message) ParameterParser.getPlainMessage(req, false);
        }
        Appendix.PublicKeyAnnouncement publicKeyAnnouncement = null;
        String recipientPublicKey = Convert.emptyToNull(req.getParameter("recipientPublicKey"));
        if (recipientPublicKey != null) {
            publicKeyAnnouncement = new Appendix.PublicKeyAnnouncement(Convert.parseHexString(recipientPublicKey));
        }

        Appendix.Phasing phasing = null;
        boolean phased = "true".equalsIgnoreCase(req.getParameter("phased"));
        if (phased) {
            phasing = parsePhasing(req);
        }

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE;
        } else if (deadlineValue == null) {
            return MISSING_DEADLINE;
        }

        short deadline;
        try {
            deadline = Short.parseShort(deadlineValue);
            if (deadline < 1) {
                return INCORRECT_DEADLINE;
            }
        } catch (NumberFormatException e) {
            return INCORRECT_DEADLINE;
        }

        long feeNQT = ParameterParser.getFeeNQT(req);
        int ecBlockHeight = ParameterParser.getInt(req, "ecBlockHeight", 0, Integer.MAX_VALUE, false);
        long ecBlockId = ParameterParser.getUnsignedLong(req, "ecBlockId", false); //Verificare griggio
        //Logger.logMessage("ecBlockId="+ecBlockId+" | ecBlockHeight="+ecBlockHeight+" | Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight)="+Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight));
        //ecBlockId = new BigInteger("429943628837330866").longValue();
        if (ecBlockId != 0 && ecBlockId != Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight)) {
        	//suse 10/03/2017
        	JSONObject response = new JSONObject();
            response.put("errorCode", 4);
            response.put("errorDescription", "Incorrect ecBlockId="+ecBlockId+" does not match the block id at ecBlockHeight="+ecBlockHeight+" - Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight)="+Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight));
            return JSON.prepare(response);
        	//JSONStreamAware msg = incorrect("ecBlockId", "ecBlockId does not match the block id at ecBlockHeight");
        	//var message = JSONStreamAware (incorrect"Incorrect ecBlockId="+ecBlockId+" does not match the block id at ecBlockHeight="+ecBlockHeight+" - Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight)="+Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight); 
        	//return 
            //return INCORRECT_EC_BLOCK;
        }
        if (ecBlockId == 0 && ecBlockHeight > 0) {
            ecBlockId = Apo.getBlockchain().getBlockIdAtHeight(ecBlockHeight);
        }

        JSONObject response = new JSONObject();

        // shouldn't try to get publicKey from senderAccount as it may have not been set yet
        byte[] publicKey = secretPhrase != null ? Crypto.getPublicKey(secretPhrase) : Convert.parseHexString(publicKeyValue);

        try {
            Transaction.Builder builder = Apo.newTransactionBuilder(publicKey, amountNQT, feeNQT,
                    deadline, attachment).referencedTransactionFullHash(referencedTransactionFullHash);
            if (attachment.getTransactionType().canHaveRecipient()) {
                builder.recipientId(recipientId);
            }
            builder.appendix(encryptedMessage);
            builder.appendix(message);
            builder.appendix(publicKeyAnnouncement);
            builder.appendix(encryptToSelfMessage);
            builder.appendix(phasing);
            builder.appendix(prunablePlainMessage);
            builder.appendix(prunableEncryptedMessage);
            if (ecBlockId != 0) {
                builder.ecBlockId(ecBlockId);
                builder.ecBlockHeight(ecBlockHeight);
            }
            Transaction transaction = builder.build(secretPhrase);

            //Logger.logMessage("-------------->>>>>>>>>>>>>>>>> transaction.getFeeNQT()="+transaction.getFeeNQT());
            try {
                if (Math.addExact(amountNQT, transaction.getFeeNQT()) > senderAccount.getUnconfirmedBalanceNQT()) {
                    return NOT_ENOUGH_FUNDS;
                }
            } catch (ArithmeticException e) {
                return NOT_ENOUGH_FUNDS;
            }
            JSONObject transactionJSON = JSONData.unconfirmedTransaction(transaction);
            response.put("transactionJSON", transactionJSON);
            try {
                response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
            } catch (ApoException.NotYetEncryptedException ignore) {}
            if (secretPhrase != null) {
                response.put("transaction", transaction.getStringId());
                response.put("fullHash", transactionJSON.get("fullHash"));
                response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
                response.put("signatureHash", transactionJSON.get("signatureHash"));
            }
            if (broadcast) {
                Apo.getTransactionProcessor().broadcast(transaction);
                response.put("broadcasted", true);
            } else {
                transaction.validate();
                response.put("broadcasted", false);
            }
        } catch (ApoException.NotYetEnabledException e) {
            return FEATURE_NOT_AVAILABLE;
        } catch (ApoException.InsufficientBalanceException e) {
            throw e;
        } catch (ApoException.ValidationException e) {
            if (broadcast) {
                response.clear();
            }
            response.put("broadcasted", false);
            JSONData.putException(response, e);
        }
        return response;

    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected final boolean allowRequiredBlockParameters() {
        return false;
    }

}
