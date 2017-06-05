package enterprises.mccollum.sauth;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.Query;

import enterprises.mccollum.utils.genericentityejb.GenericPersistenceManager;

@Local
@Stateless
public class TempKeyBean extends GenericPersistenceManager<TempKey, Long> {
	public TempKeyBean(){
		super(TempKey.class);
	}

	public void expireOld() {
		Query q = em.createQuery("DELETE FROM "+tableName+" data WHERE data.expirationDate < :expirationDate");
		q.setParameter("expirationDate", System.currentTimeMillis());
		q.executeUpdate();
	}
	
	public TempKey getByKey(String key){
		Query q = em.createQuery("SELECT data FROM "+tableName+" data where data.keyStr = :keyStr");
		q.setParameter("keyStr", key);
		return (TempKey)q.getSingleResult();
	}
}
