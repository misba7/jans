import os
import re
import sys
import datetime

from setup_app.static import AppType, InstallOption
from setup_app.config import Config
from setup_app.utils import base
from setup_app.installers.base import BaseInstaller
from setup_app.utils.setup_utils import SetupUtils

class RDBMInstaller(BaseInstaller, SetupUtils):

    def __init__(self):
        self.service_name = 'rdbm-server'
        self.app_type = AppType.APPLICATION
        self.install_type = InstallOption.OPTONAL
        self.install_var = 'rdbm_install'
        self.register_progess()

        self.output_dir = os.path.join(Config.outputFolder, Config.rdbm_type)

    def install(self):
        self.create_tables()
        self.create_indexes()
        self.rdbmProperties()

    def create_tables(self):

        tables = []

        for jans_schema_fn_ in ('jans_schema.json', 'custom_schema.json'):
            jans_schema_fn = os.path.join(Config.install_dir, 'schema', jans_schema_fn_)
            jans_schema = base.readJsonFile(jans_schema_fn)

            for obj in jans_schema['objectClasses']:
                sql_tbl_name = obj['names'][0]
                sql_tbl_cols = []

                for attrname in obj['may']:
                    if attrname in self.dbUtils.sql_data_types:
                        type_ = self.dbUtils.sql_data_types[attrname]
                        if type_[Config.rdbm_type]['type'] == 'VARCHAR':
                            if type_[Config.rdbm_type]['size'] <= 127:
                                data_type = 'VARCHAR({})'.format(type_[Config.rdbm_type]['size'])
                            elif type_[Config.rdbm_type]['size'] <= 255:
                                data_type = 'TINYTEXT'
                            else:
                                data_type = 'TEXT'
                        else:
                            data_type = type_[Config.rdbm_type]['type']

                    else:
                        attr_syntax = self.dbUtils.get_attr_syntax(attrname)
                        type_ = self.dbUtils.ldap_sql_data_type_mapping[attr_syntax]
                        if type_[Config.rdbm_type]['type'] == 'VARCHAR':
                            data_type = 'VARCHAR({})'.format(type_[Config.rdbm_type]['size'])
                        else:
                            data_type = type_[Config.rdbm_type]['type']

                    sql_tbl_cols.append('`{}` {}'.format(attrname, data_type))

                sql_cmd = 'CREATE TABLE `{}` (`id` int NOT NULL auto_increment, `doc_id` VARCHAR(48) NOT NULL UNIQUE, `objectClass` VARCHAR(48), dn VARCHAR(128), {}, PRIMARY KEY  (`id`, `doc_id`));'.format(sql_tbl_name, ', '.join(sql_tbl_cols))
                self.dbUtils.exec_rdbm_query(sql_cmd)
                tables.append(sql_cmd)

        self.writeFile(os.path.join(self.output_dir, 'jans_tables.sql'), '\n'.join(tables))

    def create_indexes(self):
        indexes = []

        sql_indexes_fn = os.path.join(Config.static_rdbm_dir, 'sql_index.json')
        sql_indexes = base.readJsonFile(sql_indexes_fn)

        for table in sql_indexes[Config.rdbm_type]:
            for field in sql_indexes[Config.rdbm_type][table]['fields']:
                sql_cmd = 'ALTER TABLE {0}.{1} ADD INDEX `{1}_{2}` (`{3}`);'.format(
                                Config.rdbm_db,
                                table,
                                re.sub(r'[^0-9a-zA-Z\s]+','_', field),
                                field
                                )
                self.dbUtils.exec_rdbm_query(sql_cmd)
                indexes.append(sql_cmd)

            for i, custom in enumerate(sql_indexes[Config.rdbm_type][table]['custom']):
                sql_cmd = 'ALTER TABLE {0}.{1} ADD INDEX `{1}_{2}`(({3}));'.format(
                    Config.rdbm_db,
                    table,
                    i,
                    custom
                    )
                self.dbUtils.exec_rdbm_query(sql_cmd)
                indexes.append(sql_cmd)

        self.writeFile(os.path.join(self.output_dir, 'jans_indexes.sql'), '\n'.join(indexes))


    def rdbmProperties(self):
        Config.rdbm_password_enc = self.obscure(Config.rdbm_password)
        Config.templateRenderingDict['server_time_zone'] = str(datetime.datetime.now(datetime.timezone(datetime.timedelta(0))).astimezone().tzinfo)
        self.renderTemplateInOut(Config.jansRDBMProperties, Config.templateFolder, Config.configFolder)

    def create_folders(self):
        self.createDirs(Config.static_rdbm_dir)

    def installed(self):
        # to be implemented
        return True
