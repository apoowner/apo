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

package apo;

import apo.db.BasicDb;
import apo.db.TransactionalDb;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "apo.testDb" : "apo.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(Apo.getIntProperty("apo.dbCacheKB"))
            .dbUrl(Apo.getStringProperty(PREFIX + "Url"))
            .dbType(Apo.getStringProperty(PREFIX + "Type"))
            .dbDir(Apo.getStringProperty(PREFIX + "Dir"))
            .dbParams(Apo.getStringProperty(PREFIX + "Params"))
            .dbUsername(Apo.getStringProperty(PREFIX + "Username"))
            .dbPassword(Apo.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(Apo.getIntProperty("apo.maxDbConnections"))
            .loginTimeout(Apo.getIntProperty("apo.dbLoginTimeout"))
            .defaultLockTimeout(Apo.getIntProperty("apo.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(Apo.getIntProperty("apo.dbMaxMemoryRows"))
    );

    static void init() {
        db.init(new ApoDbVersion());
    }

    static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
