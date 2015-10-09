package a.b.imgurandroid.android.api.pojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class GalleryData{

    @SerializedName("data")
    @Expose
    private List<ImageData> data = new ArrayList<ImageData>();
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("status")
    @Expose
    private Integer status;

    /**
     *
     * @return
     * The data
     */
    public List<ImageData> getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(List<ImageData> data) {
        this.data = data;
    }

    /**
     *
     * @return
     * The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     *
     * @return
     * The status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

}