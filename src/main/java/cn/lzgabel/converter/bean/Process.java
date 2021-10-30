package cn.lzgabel.converter.bean;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 〈功能简述〉<br>
 * 〈〉
 *
 * @author lizhi
 * @date 2021-10-30
 * @since 1.0.0
 */

@Data
@SuperBuilder
public class Process {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程名称
     */
    private String name;
}
