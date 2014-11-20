package com.aquent.globalconfig;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.servlet.ServletToolInfo;

public class GlobalConfigViewtoolInfo extends ServletToolInfo {

    @Override
    public String getKey () {
        return "gconfig";
    }

    @Override
    public String getScope () {
        return ViewContext.APPLICATION;
    }

    @Override
    public String getClassname () {
        return GlobalConfigViewtool.class.getName();
    }

    @Override
    public Object getInstance ( Object initData ) {

        GlobalConfigViewtool viewTool = new GlobalConfigViewtool();
        viewTool.init( initData );

        setScope( ViewContext.APPLICATION );

        return viewTool;
    }

}
