FROM php:7.3-fpm

RUN apt-get update --fix-missing && apt-get install -y \
    git wget zip unzip nodejs
RUN wget https://phar.phpunit.de/phpunit.phar \
&& chmod +x phpunit.phar \
&& mv phpunit.phar /usr/local/bin/phpunit
RUN wget https://getcomposer.org/composer.phar \
&& chmod +x composer.phar \
&& mv composer.phar /usr/local/bin/composer
