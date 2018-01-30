package apo;

import apo.Constants;
import apo.util.Logger;

public final class Coinbase {
  public static long computeCoinbase(long height) {
    long elapsedPeriods = (long) (height / Constants.COINBASE_PERIOD);
    long totalCoinbase = 0;
    float factor = 1.0f;
    for(int i = 0; i < elapsedPeriods; i++) {
      totalCoinbase += ((long) (Constants.COINBASE_INITIAL * factor)) * Constants.COINBASE_PERIOD;
      factor *= Constants.COINBASE_FACTOR;
    }
    long currentCoinbase = ((long) (Constants.COINBASE_INITIAL * factor));
    totalCoinbase += currentCoinbase * (height - (Constants.COINBASE_PERIOD * elapsedPeriods));
    long payout = (totalCoinbase >= Constants.COINBASE_MAX) ? 0 : Math.min(currentCoinbase, Constants.COINBASE_MAX - totalCoinbase);
    // Logger.logMessage("Computed Coinbase for Block Height " + height + " = " + payout);
    return payout * Constants.ONE_APO;
  }
}