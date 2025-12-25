package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.entity.Role;
import com.smileshark.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;


    /**
     * 添加AI角色
     * <p>
     * 由于每个店铺只能有一个AI客服，所以在添加时会对商家的id做一个检查，防止一个商家有多个AI客服
     * </p>
     * @param role 角色信息
     * @return Result 操作结果
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Role role) {
        return roleService.add(role);
    }

    /**
     * 删除AI角色
     * @param id 角色id
     * @return Result 操作结果
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Integer id) {
        return roleService.delete(id);
    }

    /**
     * 修改AI角色
     * @param role 角色信息
     * @return Result 操作结果
     */
    @PostMapping("/update")
    public Result<?> update(@RequestBody Role role) {
        return roleService.update(role);
    }

    /**
     * 直接查询店铺AI的详细信息
     * @param ctId 商户id
     * @return 角色信息
     */
    @GetMapping("/detailsByCtId")
    public Result<?> detailsByCtId(@RequestParam Integer ctId) {
        return roleService.detailsByCtId(ctId);
    }
}
