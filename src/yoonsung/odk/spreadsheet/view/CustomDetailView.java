package yoonsung.odk.spreadsheet.view;

import java.util.HashMap;
import java.util.Map;
import yoonsung.odk.spreadsheet.Activity.SpreadSheet;
import yoonsung.odk.spreadsheet.Activity.TableActivity;
import yoonsung.odk.spreadsheet.data.ColumnProperties;
import yoonsung.odk.spreadsheet.data.DbHelper;
import yoonsung.odk.spreadsheet.data.DbTable;
import yoonsung.odk.spreadsheet.data.Query;
import yoonsung.odk.spreadsheet.data.Table;
import yoonsung.odk.spreadsheet.data.TableProperties;
import yoonsung.odk.spreadsheet.data.UserTable;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A view for displaying a customizable detail view of a row of data.
 * 
 * @author hkworden
 */
public class CustomDetailView extends WebView {
    
    private static final String DEFAULT_HTML =
        "<html><body>" +
        "<p>No detail view has been specified.</p>" +
        "</body></html>";
    
    private TableProperties tp;
    private Control control;
    private RowData jsData;
    
    public CustomDetailView(Context context, TableProperties tp) {
        super(context);
        this.tp = tp;
        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient() {});
        control = new Control(context);
        addJavascriptInterface(control, "control");
        jsData = new RowData(tp);
        addJavascriptInterface(jsData, "data");
    }
    
    public void display(String rowId, Map<String, String> data) {
        jsData.set(data);
        String filename = tp.getDetailViewFilename();
        if (filename != null) {
            loadUrl("file:///" + filename);
        } else {
            loadData(DEFAULT_HTML, "text/html", null);
        }
    }
    
    /**
     * "Unused" warnings are suppressed because the public methods of this
     * class are meant to be called through the JavaScript interface.
     */
    private class RowData {
        
        private final TableProperties tp;
        private Map<String, String> data;
        
        RowData(TableProperties tp) {
            this.tp = tp;
        }
        
        RowData(TableProperties tp, Map<String, String> data) {
            this.tp = tp;
            this.data = data;
        }
        
        void set(Map<String, String> data) {
            this.data = data;
        }
        
        @SuppressWarnings("unused")
        public String get(String key) {
            ColumnProperties cp = tp.getColumnByUserLabel(key);
            if (cp == null) {
                return null;
            }
            return data.get(cp.getColumnDbName());
        }
    }
    
    /**
     * "Unused" warnings are suppressed because the public methods of this
     * class are meant to be called through the JavaScript interface.
     */
    private class TableData {
        
        private final TableProperties tp;
        private final Table table;
        
        public TableData(TableProperties tp, Table table) {
            this.tp = tp;
            this.table = table;
        }
        
        @SuppressWarnings("unused")
        public int getCount() {
            return table.getHeight();
        }
        
        @SuppressWarnings("unused")
        public RowData getData(int index) {
            Map<String, String> data = new HashMap<String, String>();
            for (int i = 0; i < table.getWidth(); i++) {
                data.put(table.getHeader(i), table.getData(index, i));
            }
            return new RowData(tp, data);
        }
    }
    
    private class Control {
        
        private Context context;
        private TableProperties[] allTps;
        private Map<String, TableProperties> tpMap;
        
        public Control(Context context) {
            this.context = context;
        }
        
        private void initTpInfo() {
            if (tpMap != null) {
                return;
            }
            tpMap = new HashMap<String, TableProperties>();
            allTps = TableProperties.getTablePropertiesForAll(
                    DbHelper.getDbHelper(context));
            for (TableProperties tp : allTps) {
                tpMap.put(tp.getDisplayName(), tp);
            }
        }
        
        @SuppressWarnings("unused")
        public boolean openTable(String tableName, String query) {
            initTpInfo();
            if (!tpMap.containsKey(tableName)) {
                return false;
            }
            Intent intent = new Intent(context, SpreadSheet.class);
            intent.putExtra(TableActivity.INTENT_KEY_TABLE_ID,
                    tpMap.get(tableName).getTableId());
            intent.putExtra(TableActivity.INTENT_KEY_QUERY, query);
            context.startActivity(intent);
            return true;
        }
        
        @SuppressWarnings("unused")
        public TableData query(String tableName, String searchText) {
            initTpInfo();
            if (!tpMap.containsKey(tableName)) {
                return null;
            }
            TableProperties tp = tpMap.get(tableName);
            Query query = new Query(allTps, tp);
            query.loadFromUserQuery(searchText);
            DbTable dbt = DbTable.getDbTable(DbHelper.getDbHelper(context),
                    tp.getTableId());
            return new TableData(tp, dbt.getRaw(query, tp.getColumnOrder()));
        }
    }
}
