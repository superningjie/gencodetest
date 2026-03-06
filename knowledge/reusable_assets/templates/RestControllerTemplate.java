package {{packageName}}.controller;

import com.kingdee.hr.common.util.Result;
import {{packageName}}.service.{{serviceName}};
import {{packageName}}.dto.{{moduleName}}DTO;
import {{packageName}}.vo.{{moduleName}}VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * {{moduleName}}管理Controller
 * 自动生成时间: {{generateTime}}
 */
@RestController
@RequestMapping("{{basePath}}")
public class {{className}}Controller {

    @Autowired
    private {{serviceName}} {{serviceName | lowerFirst}};

    /**
     * 查询列表
     */
    @GetMapping("/list")
    public Result<List<{{moduleName}}VO>> list() {
        return Result.success({{serviceName | lowerFirst}}.list());
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<{{moduleName}}VO> getById(@PathVariable Long id) {
        return Result.success({{serviceName | lowerFirst}}.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> create(@RequestBody @Valid {{moduleName}}DTO dto) {
        {{serviceName | lowerFirst}}.create(dto);
        return Result.success();
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid {{moduleName}}DTO dto) {
        {{serviceName | lowerFirst}}.update(id, dto);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        {{serviceName | lowerFirst}}.delete(id);
        return Result.success();
    }
}
