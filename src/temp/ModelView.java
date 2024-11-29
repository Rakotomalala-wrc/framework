package frameworks;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public ModelView() {
        this.data = new HashMap<>();
    }

    public ModelView(String url) {
        this.url = url;
        this.data = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }
}
