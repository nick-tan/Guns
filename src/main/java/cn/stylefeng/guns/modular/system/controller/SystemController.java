/**
 * Copyright 2018-2020 stylefeng & fengshuonan (https://gitee.com/stylefeng)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.stylefeng.guns.modular.system.controller;

import cn.hutool.core.codec.Base64;
import cn.stylefeng.guns.core.common.constant.DefaultAvatar;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.shiro.ShiroUser;
import cn.stylefeng.guns.modular.system.model.FileInfo;
import cn.stylefeng.guns.modular.system.model.User;
import cn.stylefeng.guns.modular.system.service.IFileInfoService;
import cn.stylefeng.guns.modular.system.service.IUserService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.RequestEmptyException;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import cn.stylefeng.roses.kernel.model.exception.enums.CoreExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 通用控制器
 *
 * @author fengshuonan
 * @Date 2017年2月17日20:27:22
 */
@Controller
@RequestMapping("/system")
@Slf4j
public class SystemController extends BaseController {

    private String PREFIX = "/common/";

    @Autowired
    private IUserService userService;

    @Autowired
    private IFileInfoService fileInfoService;

    /**
     * 通用的树列表选择器
     */
    @RequestMapping("/commonTree")
    public String deptTreeList(@RequestParam("formName") String formName,
                               @RequestParam("formId") String formId,
                               @RequestParam("treeUrl") String treeUrl, Model model) {

        if (ToolUtil.isOneEmpty(formName, formId, treeUrl)) {
            throw new RequestEmptyException("请求数据不完整！");
        }

        try {
            model.addAttribute("formName", URLDecoder.decode(formName, "UTF-8"));
            model.addAttribute("formId", URLDecoder.decode(formId, "UTF-8"));
            model.addAttribute("treeUrl", URLDecoder.decode(treeUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RequestEmptyException("请求数据不完整！");
        }

        return PREFIX + "common_tree_dlg.html";
    }

    /**
     * 上传头像
     *
     * @author fengshuonan
     * @Date 2018/11/9 12:45 PM
     */
    @RequestMapping("/uploadAvatar")
    @ResponseBody
    public Object uploadAvatar(@RequestParam String avatar) {

        if (ToolUtil.isEmpty(avatar)) {
            throw new RequestEmptyException("请求头像为空");
        }

        avatar = avatar.substring(avatar.indexOf(",") + 1);

        fileInfoService.uploadAvatar(avatar);

        return SUCCESS_TIP;
    }

    /**
     * 预览头像
     *
     * @author fengshuonan
     * @Date 2018/11/9 12:45 PM
     */
    @RequestMapping("/previewAvatar")
    @ResponseBody
    public Object previewAvatar(HttpServletResponse response) {

        ShiroUser currentUser = ShiroKit.getUser();
        if (currentUser == null) {
            throw new ServiceException(CoreExceptionEnum.NO_CURRENT_USER);
        }

        User user = userService.selectById(currentUser.getId());

        String avatar = user.getAvatar();
        if (ToolUtil.isEmpty(avatar)) {
            avatar = DefaultAvatar.BASE_64_AVATAR;
        } else {
            FileInfo fileInfo = fileInfoService.selectById(avatar);
            avatar = fileInfo.getFileData();
        }

        //输出图片的文件流
        try {
            response.setContentType("image/jpeg");
            byte[] decode = Base64.decode(avatar);
            response.getOutputStream().write(decode);
        } catch (IOException e) {
            log.error("获取图片的流错误！", avatar);
            throw new ServiceException(CoreExceptionEnum.SERVICE_ERROR);
        }

        return null;
    }

}
