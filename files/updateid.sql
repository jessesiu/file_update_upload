select setval('file_attributes_id_seq',(select max(id) from file_attributes));
select setval('file_experiment_id_seq',(select max(id) from file_experiment));
select setval('file_relationship_id_seq',(select max(id) from file_relationship));
select setval('file_sample_id_seq',(select max(id) from file_sample));
select setval('file_id_seq',(select max(id) from file ));
select setval('file_type_id_seq',(select max(id) from file_type ));
select setval('file_format_id_seq',(select max(id) from file_format ));
select setval('attribute_id_seq',(select max(id) from attribute));