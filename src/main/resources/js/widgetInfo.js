/**
 * Created by lhx on 2016/8/11.
 */
CMSWidgets.initWidget({
// 编辑器相关
    editor: {
        saveComponent: function (onFailed) {
            this.properties.BgColor = $(".BgColor").val();
            this.properties.titleBgColor = $(".titleBgColor").val();
            if (this.properties.classCategorySerial == undefined || this.properties.classCategorySerial == '') {
                onFailed('商城类目数据源不能为空');
                return;
            }
        }
    }
});
