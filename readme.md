# Mensascrap2

![image](https://img.shields.io/badge/Clojure-5881D8?style=for-the-badge&logo=clojure&logoColor=white)

*A Clojure library and data source designed to provide easily parseable meal data for the canteens in Karlsruhe, Germany.*

↓ Showcase Image

![Showcase](https://user-images.githubusercontent.com/82055622/209588571-310b3e87-a39f-4baf-b2bd-7a9348dccd44.png)


## Usage

This application will run in regular intervals via github actions.
A parseable datasource will be provided via github pages.

To consume that data:
1. Pull the data from the [data source](https://gahliadhbw.github.io/mensascrap/master.json)
2. Parse it into a native data structure you can work with. Prefer extensible datastructures (flexible array lengths).
3. Build your application
