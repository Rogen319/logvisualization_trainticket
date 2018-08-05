package security.service;

import org.springframework.http.HttpHeaders;
import security.domain.*;

public interface SecurityService {

    GetAllSecurityConfigResult findAllSecurityConfig();

    CreateSecurityConfigResult addNewSecurityConfig(CreateSecurityConfigInfo info);

    UpdateSecurityConfigResult modifySecurityConfig(UpdateSecurityConfigInfo info);

    DeleteConfigResult deleteSecurityConfig(DeleteConfigInfo info);

    CheckResult check(CheckInfo info,HttpHeaders headers);

}
