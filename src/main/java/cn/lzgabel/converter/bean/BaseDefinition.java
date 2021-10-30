package cn.lzgabel.converter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 〈功能简述〉<br>
 * 〈基础元素定义〉
 *
 * @author lizhi
 * @date 2021-10-30
 * @since 1.0.0
 */

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseDefinition implements Serializable {

    private String nodeName;

    private static String nodeType;

    private BaseDefinition nextNode;

    public abstract String getNodeType();

    public abstract static class BaseDefinitionBuilder<C extends BaseDefinition, B extends BaseDefinition.BaseDefinitionBuilder<C, B>> {
        public B nodeNode(String nodeName) {
            this.nodeName = nodeName;
            return self();
        }

        public B nextNode(BaseDefinition nextNode) {
            this.nextNode = nextNode;
            return self();
        }

    }
}
