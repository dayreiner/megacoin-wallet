package de.schildbach.wallet.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schildbach.wallet.Constants;

/**
 * Created by arivera on 6/11/2014.
 */
public class BlockchainServiceBootstrapper {
    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceBootstrapper.class);

    private Context _context;
    private Intent _serviceIntent;
    private PendingIntent _alarmIntent;
    private AlarmManager _alarmManager;

    public BlockchainServiceBootstrapper(Context context){
        _context = context;
        _serviceIntent = new Intent(BlockchainService.ACTION_HOLD_WIFI_LOCK, null, _context, BlockchainServiceImpl.class);
        _alarmIntent = PendingIntent.getService(_context, 0, _serviceIntent, 0);
        _alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
    }

    public void Start(){

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

        final long prefsLastUsed = prefs.getLong(Constants.PREFS_KEY_LAST_USED, 0);

        _context.startService(_serviceIntent);

        final long now = System.currentTimeMillis();

        final long lastUsedAgo = now - prefsLastUsed;
        final long alarmInterval;
        if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_JUST_MS)
            alarmInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        else if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_RECENTLY_MS)
            alarmInterval = AlarmManager.INTERVAL_HALF_DAY;
        else
            alarmInterval = AlarmManager.INTERVAL_DAY;

        log.info("last used {} minutes ago, rescheduling sync in roughly {} minutes", lastUsedAgo / DateUtils.MINUTE_IN_MILLIS, alarmInterval
                / DateUtils.MINUTE_IN_MILLIS);

        _alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now, alarmInterval, _alarmIntent);
    }

    public void Stop(){
        _context.stopService(_serviceIntent);
        _alarmManager.cancel(_alarmIntent);
    }
}
