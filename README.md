# Проект «Обмен валют»

REST API для описания валют и обменных курсов. Позволяет просматривать и редактировать списки валют и обменных курсов,
и совершать расчёт конвертации произвольных сумм из одной валюты в другую.  
_Веб-интерфейс для проекта не подразумевается._

---

Приложение написано на языке Java в ООП-стиле с использованием:
- Maven
- Servlets
- REST API
- SQLite
- Tomcat 10
- Deploy in VPS (Ubuntu 20.04)

Техническое задание проекта: https://zhukovsd.github.io/java-backend-learning-course/Projects/CurrencyExchange/

---

## Реализованные запросы:

* GET-запросы
  * /currencies – получение списка валют
  * /currency/EUR – получение конкретной валюты
  * /exchangeRates – получение списка всех обменных курсов
  * /exchangeRate/USDRUB – Получение конкретного обменного курса
  

* POST-запросы
  * добавление новой валюты в базу
    * /currencies?name=US Dollar&code=USD&sign=$
  * добавление нового обменного курса в базу
    * /exchangeRates?baseCurrencyCode=USD&targetCurrencyCode=RUB&rate=77

  
* PATCH-запрос
  * /exchangeRate/USDRUB?rate=70 – обновление существующего в базе обменного курса

Ссылка на приложение: http://185.21.142.32:8080/currency_exchanger-1.0/

___

