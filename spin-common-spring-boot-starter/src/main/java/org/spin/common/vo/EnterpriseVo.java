package org.spin.common.vo;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
/**
 * 企业对象
 * @author Darrick
 */
public class EnterpriseVo implements Serializable {

    @ApiModelProperty(value = "机构ID", name = "organId", example = "84123455875425874", dataType = "Long")
    private Long organId;

    @ApiModelProperty(value = "机构名称", name = "organName", example = "技术中心", dataType = "String")
    private String organName;

    public EnterpriseVo() {
    }

    public EnterpriseVo(Long organId, String organName) {
        this.organId = organId;
        this.organName = organName;
    }

    public Long getOrganId() {
        return organId;
    }

    public void setOrganId(Long organId) {
        this.organId = organId;
    }

    public String getOrganName() {
        return organName;
    }

    public void setOrganName(String organName) {
        this.organName = organName;
    }
}
