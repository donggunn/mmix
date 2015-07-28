package eddie.wu.domain.loop;

/**
 * 围棋的规则相当简单明了，唯一的例外可能就设计打劫的部分。<br/>
 * 弃权即放弃着手权，让对方下。虚手也是这个意识。另外在禁着点下棋，效果等同。视同弃权。<br/>
 * 实战中双方弃权，对局暂停，如果双方对于棋局的认识（限于死活）没有出入，则正式终局。<br/>
 * 若有出入，重启对局判定死活，但是不能走其它之前没有发现的手段。举一个麻烦的例子；<br/>
 * 死活未定时，双方弃权后不得再加以改变；按现状计算官子。只有象盘角曲四这样的局面。<br/>
 * 死活已定的，才可以提证死活，因为他们是先后手无关的。<br/>
 * 计算机围棋中为求简单，避免不必要的繁琐。要求双方将死子提尽后终局。提尽死子对人类棋手而言<br/>
 * 过于麻烦，但是对计算机而言没有困难。
 * 
 * @author Eddie
 * 
 */
public class Rule {

}