package wang.liangchen.matrix.bpmjob.domain.host;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

/**
 * @author Liangchen.Wang 2023-03-17 17:17
 */
@Entity(name = "bpmjob_host")
public class Host extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(value = IdStrategy.Strategy.MatrixFlake)
    private Long hostId;

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
}
