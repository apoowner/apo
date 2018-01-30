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

package apo.addons;

import apo.Account;
import apo.BlockchainProcessor;
import apo.Apo;
import apo.util.Convert;
import apo.util.Logger;

public final class Demo implements AddOn {

    @Override
    public void init() {
        Apo.getBlockchainProcessor().addListener(block -> Logger.logInfoMessage("Block " + block.getStringId()
                + " has been forged by account " + Convert.rsAccount(block.getGeneratorId()) + " having effective balance of "
                + Account.getAccount(block.getGeneratorId()).getEffectiveBalanceAPO()),
                BlockchainProcessor.Event.BEFORE_BLOCK_APPLY);
    }

    @Override
    public void shutdown() {
        Logger.logInfoMessage("Goodbye!");
    }

}