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
import apo.Poll;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;


public final class GetPoll extends APIServlet.APIRequestHandler {

    static final GetPoll instance = new GetPoll();

    private GetPoll() {
        super(new APITag[] {APITag.VS}, "poll");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ApoException {
        Poll poll = ParameterParser.getPoll(req);
        return JSONData.poll(poll);
    }
}