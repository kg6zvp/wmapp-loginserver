package enterprises.mccollum.sauth;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.Query;

import enterprises.mccollum.utils.genericentityejb.GenericPersistenceManager;

@Local
@Stateless
public class TempKeyBean extends GenericPersistenceManager<TempUserInfoKeyEntity, String> {
	public TempKeyBean(){
		super(TempUserInfoKeyEntity.class);
	}

	public void expireOld() {
		Query q = em.createQuery("DELETE FROM "+tableName+" data WHERE data.expirationDate < :expirationDate");
		q.setParameter("expirationDate", System.currentTimeMillis());
		q.executeUpdate();
	}
}
