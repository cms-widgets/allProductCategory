/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2016. All rights reserved.
 */

package com.huotu.hotcms.widget.allProductCategory;

import com.huotu.hotcms.service.common.ContentType;
import com.huotu.hotcms.service.common.SiteType;
import com.huotu.hotcms.service.entity.Link;
import com.huotu.hotcms.service.entity.MallClassCategory;
import com.huotu.hotcms.service.entity.MallProductCategory;
import com.huotu.hotcms.service.exception.PageNotFoundException;
import com.huotu.hotcms.service.model.MallClassCategoryModel;
import com.huotu.hotcms.service.repository.LinkRepository;
import com.huotu.hotcms.service.repository.MallClassCategoryRepository;
import com.huotu.hotcms.service.repository.MallProductCategoryRepository;
import com.huotu.hotcms.service.service.CategoryService;
import com.huotu.hotcms.widget.*;
import com.huotu.hotcms.widget.entity.PageInfo;
import com.huotu.hotcms.widget.service.PageService;
import me.jiangcai.lib.resource.service.ResourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @author CJ
 */
public class WidgetInfo implements Widget, PreProcessWidget {
    public static final String BG_COLOR = "BgColor";
    public static final String COLOR = "color";
    public static final String CLASS_CATEGORY_SERIAL = "classCategorySerial";
    public static final String DATA_LIST = "dataList";
    private static final Log log = LogFactory.getLog(WidgetInfo.class);

    @Override
    public String groupId() {
        return "com.huotu.hotcms.widget.allProductCategory";
    }

    @Override
    public String widgetId() {
        return "allProductCategory";
    }

    @Override
    public String name(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "全部商品分类";
        }
        return "allProductCategory";
    }

    @Override
    public String description(Locale locale) {
        if (locale.equals(Locale.CHINA)) {
            return "这是一个全部商品分类，你可以对组件进行自定义修改。";
        }
        return "This is a allProductCategory,  you can make custom change the component.";
    }

    @Override
    public String dependVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public WidgetStyle[] styles() {
        return new WidgetStyle[]{new DefaultWidgetStyle()};
    }

    @Override
    public Resource widgetDependencyContent(MediaType mediaType) {
        if (mediaType.isCompatibleWith(CSS))
            return new ClassPathResource("css/allProductCategory.css", getClass().getClassLoader());

        if (mediaType.equals(Widget.Javascript))
            return new ClassPathResource("js/widgetInfo.js", getClass().getClassLoader());
        return null;
    }

    @Override
    public Map<String, Resource> publicResources() {
        Map<String, Resource> map = new HashMap<>();
        map.put("thumbnail/defaultStyleThumbnail.png", new ClassPathResource("thumbnail/defaultStyleThumbnail.png"
                , getClass().getClassLoader()));
        return map;
    }

    @Override
    public void valid(String styleId, ComponentProperties componentProperties) throws IllegalArgumentException {
        WidgetStyle style = WidgetStyle.styleByID(this, styleId);
        //加入控件独有的属性验证
        String serial = (String) componentProperties.get(CLASS_CATEGORY_SERIAL);
        if (serial == null || serial.equals("")) {
            throw new IllegalArgumentException("控件属性缺少");
        }

    }

    @Override
    public Class springConfigClass() {
        return null;
    }

    @Override
    public ComponentProperties defaultProperties(ResourceService resourceService) throws IOException {
        ComponentProperties properties = new ComponentProperties();
        properties.put(BG_COLOR, "#0ff");
        properties.put(COLOR, "#fff");
        MallClassCategoryRepository mallClassCategoryRepository = getCMSServiceFromCMSContext(MallClassCategoryRepository.class);
        List<MallClassCategory> mallClassCategoryList = mallClassCategoryRepository.findBySite(CMSContext.RequestContext().getSite());
        if (mallClassCategoryList.isEmpty()) {
            MallClassCategory mallClassCategory = initMallClassCategory(null, initMallProductCategory());
            MallClassCategory mallClassCategory1 = initMallClassCategory(initMallClassCategory(mallClassCategory
                    , initMallProductCategory()), initMallProductCategory());
            initMallClassCategory(mallClassCategory1, initMallProductCategory());
            properties.put(CLASS_CATEGORY_SERIAL, mallClassCategory.getSerial());
        } else {
            properties.put(CLASS_CATEGORY_SERIAL, mallClassCategoryList.get(0).getSerial());
        }
        return properties;
    }

    @Override
    public void prepareContext(WidgetStyle style, ComponentProperties properties, Map<String, Object> variables
            , Map<String, String> parameters) {
        MallClassCategoryRepository mallClassCategoryRepository = getCMSServiceFromCMSContext(MallClassCategoryRepository.class);
        LinkRepository linkRepository = getCMSServiceFromCMSContext(LinkRepository.class);

        String serial = (String) properties.get(CLASS_CATEGORY_SERIAL);
        List<MallClassCategory> mallClassCategories = mallClassCategoryRepository.findByParent_Serial(serial);
        List<MallClassCategoryModel> dataList = new ArrayList<>();
        for (MallClassCategory mallClassCategory : mallClassCategories) {
            MallClassCategoryModel mallClassCategoryModel = mallClassCategory.toMallClassCategoryModel();
            if (mallClassCategoryModel.getRecommendCategory() != null) {
                List<Link> links = linkRepository.findByCategory(mallClassCategoryModel.getRecommendCategory());
                mallClassCategoryModel.setLinks(links);
            }
            setContentURI(variables, mallClassCategory);
            if (mallClassCategoryModel.isParentFlag()) {
                mallClassCategoryModel.setChildren(new ArrayList<>());
                for (MallClassCategory children : mallClassCategoryRepository
                        .findByParent_Serial(mallClassCategoryModel.getSerial())) {
                    setContentURI(variables, children);
                    MallClassCategoryModel childrenModel = children.toMallClassCategoryModel();
                    if (childrenModel.isParentFlag()) {
                        childrenModel.setChildren(new ArrayList<>());
                        for (MallClassCategory children2 : mallClassCategoryRepository.findByParent_Serial(childrenModel
                                .getSerial())) {
                            MallClassCategoryModel children2Model = children2.toMallClassCategoryModel();
                            setContentURI(variables, children2);
                            childrenModel.getChildren().add(children2Model);
                        }
                    }
                    mallClassCategoryModel.getChildren().add(childrenModel);
                }
            }
            dataList.add(mallClassCategoryModel);
        }
        variables.put(DATA_LIST, dataList);

    }

    @Override
    public SiteType supportedSiteType() {
        return SiteType.SITE_PC_SHOP;
    }

    private void setContentURI(Map<String, Object> variables, MallClassCategory mallClassCategory) {
        for (MallProductCategory mallProductCategory : mallClassCategory.getCategories()) {
            try {
                PageInfo contentPage = getCMSServiceFromCMSContext(PageService.class)
                        .getClosestContentPage(mallProductCategory, (String) variables.get("uri"));
                mallProductCategory.setContentURI(contentPage.getPagePath());
            } catch (PageNotFoundException e) {
                log.warn("...", e);
                mallProductCategory.setContentURI((String) variables.get("uri"));
            }
        }
    }


    public MallClassCategory initMallClassCategory(MallClassCategory parent, List<MallProductCategory>
            mallProductCategoryList) {
        CategoryService categoryService = getCMSServiceFromCMSContext(CategoryService.class);
        MallClassCategoryRepository mallClassCategoryRepository = getCMSServiceFromCMSContext
                (MallClassCategoryRepository.class);
        MallClassCategory mallClassCategory = new MallClassCategory();
        mallClassCategory.setContentType(ContentType.MallClass);
        mallClassCategory.setSite(CMSContext.RequestContext().getSite());
        mallClassCategory.setName("类目数据源");
        mallClassCategory.setCreateTime(LocalDateTime.now());
        mallClassCategory.setParent(parent);
        if (parent != null) {
            parent.setParentFlag(true);
            mallClassCategoryRepository.save(parent);
        }
        categoryService.init(mallClassCategory);
        mallClassCategory.setCategories(mallProductCategoryList);
        mallClassCategoryRepository.save(mallClassCategory);
        return mallClassCategory;
    }

    public List<MallProductCategory> initMallProductCategory() {
        CategoryService categoryService = getCMSServiceFromCMSContext(CategoryService.class);
        MallProductCategory mallProductCategory = new MallProductCategory();
        mallProductCategory.setSite(CMSContext.RequestContext().getSite());
        mallProductCategory.setCreateTime(LocalDateTime.now());
        mallProductCategory.setName("xxx");
        mallProductCategory.setContentType(ContentType.MallProduct);
        mallProductCategory.setGoodTitle("iphone");
        MallProductCategoryRepository mallProductCategoryRepository = getCMSServiceFromCMSContext(MallProductCategoryRepository.class);
        categoryService.init(mallProductCategory);
        mallProductCategoryRepository.save(mallProductCategory);
        List<MallProductCategory> list = new ArrayList<>();
        list.add(mallProductCategory);
        return list;
    }


}
