package enterprises.mccollum.wmapp.loginserver.jax;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserToken;

public class LoopResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext arg0, ContainerResponseContext arg1) throws IOException {
		/*Object o = arg1.getEntity();
		if(o instanceof UserToken){
			UserToken u = (UserToken)o;
			for(UserGroup g : u.getGroups())
				g.setUsers(null);
			arg1.setEntity(u);
			return;
		}else if(o instanceof UserGroup){
			UserGroup g = (UserGroup)o;
			g.setUsers(null);
			arg1.setEntity(g);
			return;
		}*/
	}

}
