/**
 * 使用 Apache Common VFS 取回各式資源範例
 */
public class Ch3Coz1 {


    public static void main(String[] args){

        // 北捷列車到站資料 API
        // @see http://taipeicity.github.io/traffic_realtime/
        // @see http://data.taipei/opendata/datalist/datasetMeta?oid=6556e1e8-c908-42d5-b984-b3f7337b139b
        String api = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=55ec6d6e-dc5c-4268-a725-d04cc262172b";

        // call remote api
        CrawlerPack.start().getFromJson(api);

    }
}
