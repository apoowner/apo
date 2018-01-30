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

import apo.ApoException;
import apo.http.APIServlet.APIRequestHandler;
import apo.peer.Peer;
import apo.peer.Peers;
import apo.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static apo.http.JSONResponses.MISSING_PEER;

public class AddPeer extends APIRequestHandler {

    static final AddPeer instance = new AddPeer();
    
    private AddPeer() {
        super(new APITag[] {APITag.NETWORK}, "peer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request)
            throws ApoException {
        String peerAddress = Convert.emptyToNull(request.getParameter("peer"));
        if (peerAddress == null) {
            return MISSING_PEER;
        }
        JSONObject response = new JSONObject();
        Peer peer = Peers.findOrCreatePeer(peerAddress, true);
        if (peer != null) {
            boolean isNewlyAdded = Peers.addPeer(peer, peerAddress);
            Peers.connectPeer(peer);
            response = JSONData.peer(peer);
            response.put("isNewlyAdded", isNewlyAdded);
        } else {
            response.put("errorCode", 8);
            response.put("errorDescription", "Failed to add peer");
        }
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
