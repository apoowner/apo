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
import apo.TaggedData;
import apo.db.DbIterator;
import apo.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDataTagsLike extends APIServlet.APIRequestHandler {

    static final GetDataTagsLike instance = new GetDataTagsLike();

    private GetDataTagsLike() {
        super(new APITag[] {APITag.DATA, APITag.SEARCH}, "tagPrefix", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ApoException {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        String prefix = Convert.emptyToNull(req.getParameter("tagPrefix"));
        if (prefix == null) {
            return JSONResponses.missing("tagPrefix");
        }
        if (prefix.length() < 2) {
            return JSONResponses.incorrect("tagPrefix", "tagPrefix must be at least 2 characters long");
        }

        JSONObject response = new JSONObject();
        JSONArray tagsJSON = new JSONArray();
        response.put("tags", tagsJSON);
        try (DbIterator<TaggedData.Tag> tags = TaggedData.Tag.getTagsLike(prefix, firstIndex, lastIndex)) {
            while (tags.hasNext()) {
                tagsJSON.add(JSONData.dataTag(tags.next()));
            }
        }
        return response;
    }

}
