package {{packageName}}.plugin

import com.kingdee.hr.plugin.sdk.AbstractPlugin
import com.kingdee.hr.plugin.sdk.IPlugin
import com.kingdee.hr.plugin.sdk.annotation.ExtensionPoint

/**
 * {{className}}插件
 * 插件ID: {{pluginId}}
 * 自动生成时间: {{generateTime}}
 */
class {{className}}Plugin : AbstractPlugin(), IPlugin {

    override fun onEnable() {
        // 注册扩展点实现
        {{#extensionPoints}}
        registerExtension("{{pointId}}", {{className}}())
        {{/extensionPoints}}
    }

    override fun onDisable() {
        // 清理资源
    }
}
