/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.hotcms.widget.allProductCategory;

import com.huotu.hotcms.service.entity.MallClassCategory;
import com.huotu.hotcms.service.repository.MallClassCategoryRepository;
import com.huotu.hotcms.widget.ComponentProperties;
import com.huotu.hotcms.widget.Widget;
import com.huotu.hotcms.widget.WidgetStyle;
import com.huotu.widget.test.Editor;
import com.huotu.widget.test.WidgetTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author CJ
 */
public class TestWidgetInfo extends WidgetTest {

    @Override
    protected boolean printPageSource() {
        return true;
    }

    @Override
    protected void editorWork(Widget widget, Editor editor, Supplier<Map<String, Object>> currentWidgetProperties) {
        if (widget instanceof WidgetInfo) {
            WidgetInfo widgetInfo = (WidgetInfo) widget;
            MallClassCategory mallClassCategory = widgetInfo.initMallClassCategory(null, null);
            editor.chooseCategory(WidgetInfo.CLASS_CATEGORY_SERIAL, mallClassCategory);
            Map map = currentWidgetProperties.get();
            assertThat(mallClassCategory.getSerial()).isEqualTo(map.get(WidgetInfo.CLASS_CATEGORY_SERIAL));
        }
    }

    @Override
    protected void browseWork(Widget widget, WidgetStyle style, Function<ComponentProperties, WebElement> uiChanger)
            throws IOException {
        ComponentProperties properties = widget.defaultProperties(resourceService);
        WebElement webElement = uiChanger.apply(properties);
        String classSerial = (String) properties.get(WidgetInfo.CLASS_CATEGORY_SERIAL);
        MallClassCategoryRepository mallClassCategoryRepository = widget.getCMSServiceFromCMSContext(MallClassCategoryRepository.class);
        List<MallClassCategory> mallClassCategories = mallClassCategoryRepository.findByParent_SerialAndDeletedFalse(classSerial);
        assertThat(webElement.findElements(By.className("item")).size()).isEqualTo(mallClassCategories.size());
    }

    @Override
    protected void editorBrowseWork(Widget widget, Function<ComponentProperties, WebElement> uiChanger
            , Supplier<Map<String, Object>> currentWidgetProperties) throws IOException {
        ComponentProperties properties = widget.defaultProperties(resourceService);
        WebElement webElement = uiChanger.apply(properties);

    }
}
