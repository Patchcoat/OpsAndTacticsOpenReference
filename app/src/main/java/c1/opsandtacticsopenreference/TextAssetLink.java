package c1.opsandtacticsopenreference;

/**
 * Created by c1user on 8/14/17.
 */

public class TextAssetLink {
    private String _text;
    private String _assetLink;
    private String _assetType;

    public TextAssetLink(String text, String assetLink, String assetType){
        this._text = text;
        this._assetLink = assetLink;
        this._assetType = assetType;
    }

    public String Text(){
        return _text;
    }

    public String AssetLink(){
        return _assetLink;
    }

    public String AssetType(){
        return _assetType;
    }
}
