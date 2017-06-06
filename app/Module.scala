import com.google.inject.AbstractModule
import models.DbEngine

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[DbEngine]).asEagerSingleton()
  }
}