package org.example.htmlfx.toolkits;

import org.example.htmlfx.dashboard.DashboardControl;
import org.example.htmlfx.user.Member_controller;

public interface ParentControllerAware {
    void setParentController(DashboardControl parentController);

    void setParentController(Member_controller parentController);
}
