package com.github.usedrarely.spring.rate.limit.configuration;

import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class RateLimitAnnotationBeanPostProcessor extends AbstractAdvisingBeanPostProcessor implements BeanFactoryAware {

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {

  }
}
