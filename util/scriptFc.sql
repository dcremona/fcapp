./run.sh > /dev/null 2>&1 &

--ALTER TABLE `fc_classifica` CHANGE `tot_punti_old` `tot_punti_old` DECIMAL(10,2) DEFAULT "0" 
--ALTER TABLE `fc_classifica` CHANGE `tot_punti_old` `tot_punti_old` BIGINT DEFAULT "0"


ALTER TABLE `fc_classifica` CHANGE `tot_punti` `tot_punti` int DEFAULT "0" ;
ALTER TABLE `fc_classifica` CHANGE `tot_punti_old` `tot_punti_old` int DEFAULT "0" ;
ALTER TABLE `fc_classifica` CHANGE `tot_punti_rosa` `tot_punti_rosa` int DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt` `tot_pt` int DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt_old` `tot_pt_old` int DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt_rosa` `tot_pt_rosa` int DEFAULT "0" ;
ALTER TABLE `fc_giornata` CHANGE `tot_casa` `tot_casa` int DEFAULT "0" ;
ALTER TABLE `fc_giornata` CHANGE `tot_fuori` `tot_fuori` int DEFAULT "0" ;
ALTER TABLE `fc_giornata_dett` CHANGE `voto` `voto` int DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `g` `g` int DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `ts` `ts` int DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `cs` `cs` int DEFAULT "0";
ALTER TABLE `fc_statistiche` CHANGE `media_voto` `media_voto` int DEFAULT "0";
ALTER TABLE `fc_statistiche` CHANGE `fanta_media` `fanta_media` int DEFAULT "0";
 

--NEW

ALTER TABLE `fc_classifica` CHANGE `tot_punti` `tot_punti` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_classifica` CHANGE `tot_punti_old` `tot_punti_old` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_classifica` CHANGE `tot_punti_rosa` `tot_punti_rosa` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt` `tot_pt` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt_old` `tot_pt_old` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_classifica_tot_pt` CHANGE `tot_pt_rosa` `tot_pt_rosa` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_giornata` CHANGE `tot_casa` `tot_casa` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_giornata` CHANGE `tot_fuori` `tot_fuori` DOUBLE DEFAULT "0" ;
ALTER TABLE `fc_giornata_dett` CHANGE `voto` `voto` DOUBLE DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `g` `g` DOUBLE DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `ts` `ts` DOUBLE DEFAULT "0";
ALTER TABLE `fc_pagelle` CHANGE `cs` `cs` DOUBLE DEFAULT "0";
ALTER TABLE `fc_statistiche` CHANGE `media_voto` `media_voto` DOUBLE DEFAULT "0";
ALTER TABLE `fc_statistiche` CHANGE `fanta_media` `fanta_media` DOUBLE DEFAULT "0";

ALTER TABLE fc_statistiche ADD CONSTRAINT fk_fc_giocatore_id FOREIGN KEY (id_giocatore) REFERENCES fc_giocatore(id_giocatore);

ALTER TABLE fc_giocatore
ADD img BLOB;

ALTER TABLE fc_giocatore
ADD img_small BLOB;

update fc_giocatore set flag_attivo='0' where flag_attivo='N';
update fc_giocatore set flag_attivo='1' where flag_attivo='S';
ALTER TABLE `fc_giocatore` CHANGE `flag_attivo` `flag_attivo` bit ;

update fc_giornata_dett_info set flag_invio='0' where flag_invio='N';
update fc_giornata_dett_info set flag_invio='1' where flag_invio='S';
ALTER TABLE `fc_giornata_dett_info` CHANGE `flag_invio` `flag_invio` bit ;

ALTER TABLE fc_attore
ADD admin bit;

ALTER TABLE fc_attore
ADD active bit;

ALTER TABLE fc_attore
DROP COLUMN grafica;





















select * from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)
select id_giocatore from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)

 delete from fc_statistiche where id_giocatore in (
select id_giocatore from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)
 )

delete from fc_pagelle where  id_giocatore in (
select id_giocatore from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)
)

select * from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)
delete from fc_giocatore where flag_attivo='N' and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)


 
select * from fc_giocatore where id_giocatore in (
 select id_giocatore from fc_pagelle where voto_giocatore=0
)

select * from fc_formazione where id_giocatore in 
(
 select id_giocatore from fc_giocatore where flag_attivo='N'
)

select * from fc_giocatore where id_giocatore not in 
(
 select id_giocatore from  fc_statistiche 
)

select count(*) from fc_statistiche
select count(*) from fc_giocatore

delete from fc_pagelle where id_giornata=8

select *  from fc_pagelle where voto_giocatore=0




select g.id_giornata,  i.desc_giornata_fc,i.data_giornata,a.desc_attore as casa, g.tot_casa/100 as ptCasa,g.tot_fuori/100 as ptFuori,b.desc_attore as fuori,g.gol_casa, gol_fuori
 from fc_giornata g, fc_attore a, fc_attore b, fc_giornata_info i
 where a.id_attore=g.id_attore_casa
 and b.id_attore=g.id_attore_fuori
 and i.codice_giornata=g.id_giornata
 and i.codice_giornata >=1
 and i.codice_giornata <=7
 order by g.id_giornata
 
  
 select g.id_giornata,  i.desc_giornata_fc,i.data_giornata,a.desc_attore as casa, b.desc_attore as fuori,g.gol_casa, gol_fuori
 from fc_giornata g, fc_attore a, fc_attore b, fc_giornata_info i
 where a.id_attore=g.id_attore_casa
 and b.id_attore=g.id_attore_fuori
 and i.codice_giornata=g.id_giornata
 and i.codice_giornata >=8
 and i.codice_giornata <=14
 order by g.id_giornata