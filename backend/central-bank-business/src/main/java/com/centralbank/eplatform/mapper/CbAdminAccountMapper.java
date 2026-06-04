package com.centralbank.eplatform.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.centralbank.eplatform.domain.CbAccountExtension;
import com.centralbank.eplatform.domain.CbAdminAccount;

public interface CbAdminAccountMapper
{
    int countAdminAccounts(@Param("keyword") String keyword, @Param("officeCode") String officeCode,
            @Param("role") String role);

    List<CbAdminAccount> selectAdminAccounts(@Param("keyword") String keyword, @Param("officeCode") String officeCode,
            @Param("role") String role, @Param("offset") int offset, @Param("pageSize") int pageSize);

    CbAdminAccount selectAdminAccountByUserId(@Param("userId") Long userId);

    Long selectUserIdByUsername(@Param("username") String username);

    int insertSysUser(CbAdminAccount account);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int updateSysUser(CbAdminAccount account);

    int resetPassword(@Param("userId") Long userId, @Param("password") String password);

    int deleteSysUserById(@Param("userId") Long userId);

    int deleteUserRolesByUserId(@Param("userId") Long userId);

    int insertAccountExtension(CbAccountExtension extension);

    int updateAccountExtension(CbAccountExtension extension);

    int deleteAccountExtensionByUserId(@Param("userId") Long userId);
}
