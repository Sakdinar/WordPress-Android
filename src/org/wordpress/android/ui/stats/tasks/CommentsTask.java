package org.wordpress.android.ui.stats.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.android.volley.VolleyError;
import com.wordpress.rest.RestRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.BuildConfig;
import org.wordpress.android.WordPress;
import org.wordpress.android.datasets.StatsMostCommentedTable;
import org.wordpress.android.datasets.StatsTopCommentersTable;
import org.wordpress.android.models.StatsMostCommented;
import org.wordpress.android.models.StatsTopCommenter;
import org.wordpress.android.providers.StatsContentProvider;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by nbradbury on 2/25/14.
 */
public class CommentsTask extends StatsTask {

    private final String mBlogId;

    public CommentsTask(String blogId) {
        mBlogId = StringUtils.notNullStr(blogId);
    }

    @Override
    public void run() {
        // most commented
        WordPress.restClient.getStatsMostCommented(mBlogId,
                new RestRequest.Listener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseMostCommentedResponse(response);
                    }
                },
                new RestRequest.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppLog.e(AppLog.T.STATS, error);
                    }
                });

        // top commenters
        WordPress.restClient.getStatsTopCommenters(mBlogId,
                new RestRequest.Listener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseTopCommenterResponse(response);
                    }
                },
                new RestRequest.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppLog.e(AppLog.T.STATS, error);
                    }
                });
    }

    void parseMostCommentedResponse(JSONObject response) {
        if (response == null || !response.has("result"))
            return;

        try {
            JSONArray results = response.getJSONArray("result");
            int count = results.length();

            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

            if (count > 0) {
                ContentProviderOperation op = ContentProviderOperation.newDelete(StatsContentProvider.STATS_MOST_COMMENTED_URI).withSelection("blogId=?", new String[] { mBlogId }).build();
                operations.add(op);
            }

            for (int i = 0; i < count; i++ ) {
                JSONObject result = results.getJSONObject(i);
                StatsMostCommented stat = new StatsMostCommented(mBlogId, result);
                ContentValues values = StatsMostCommentedTable.getContentValues(stat);
                ContentProviderOperation op = ContentProviderOperation.newInsert(StatsContentProvider.STATS_MOST_COMMENTED_URI).withValues(values).build();
                operations.add(op);
            }

            ContentResolver resolver = WordPress.getContext().getContentResolver();
            resolver.applyBatch(BuildConfig.STATS_PROVIDER_AUTHORITY, operations);
            resolver.notifyChange(StatsContentProvider.STATS_MOST_COMMENTED_URI, null);
        } catch (JSONException e) {
            AppLog.e(AppLog.T.STATS, e);
        } catch (RemoteException e) {
            AppLog.e(AppLog.T.STATS, e);
        } catch (OperationApplicationException e) {
            AppLog.e(AppLog.T.STATS, e);
        }
    }

    private void parseTopCommenterResponse(JSONObject response) {
        if (response == null || !response.has("result"))
            return;

        try {
            JSONArray results = response.getJSONArray("result");
            int count = results.length();

            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

            if (count > 0) {
                ContentProviderOperation op = ContentProviderOperation.newDelete(StatsContentProvider.STATS_TOP_COMMENTERS_URI).withSelection("blogId=?", new String[] { mBlogId }).build();
                operations.add(op);
            }

            for (int i = 0; i < count; i++ ) {
                JSONObject result = results.getJSONObject(i);
                StatsTopCommenter stat = new StatsTopCommenter(mBlogId, result);
                ContentValues values = StatsTopCommentersTable.getContentValues(stat);
                ContentProviderOperation op = ContentProviderOperation.newInsert(StatsContentProvider.STATS_TOP_COMMENTERS_URI).withValues(values).build();
                operations.add(op);
            }

            ContentResolver resolver = WordPress.getContext().getContentResolver();
            resolver.applyBatch(BuildConfig.STATS_PROVIDER_AUTHORITY, operations);
            resolver.notifyChange(StatsContentProvider.STATS_TOP_COMMENTERS_URI, null);
        } catch (JSONException e) {
            AppLog.e(AppLog.T.STATS, e);
        } catch (RemoteException e) {
            AppLog.e(AppLog.T.STATS, e);
        } catch (OperationApplicationException e) {
            AppLog.e(AppLog.T.STATS, e);
        }
    }

    @Override
    void parseResponse(JSONObject response) {
        // unused in this task
    }
}
