/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2013-2014, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.remote.galaxy;

import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.Migrator;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.galaxy.quasar.Grid;
import co.paralleluniverse.galaxy.quasar.Store;
import co.paralleluniverse.io.serialization.Serialization;
import co.paralleluniverse.io.serialization.kryo.KryoSerializer;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author pron
 */
@MetaInfServices
public class GlxMigrator implements Migrator {
    private final Grid grid;
    private final Store store;

    public GlxMigrator() {
        try {
            this.grid = new Grid(co.paralleluniverse.galaxy.Grid.getInstance());
            this.store = grid.store();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object registerMigratingActor() throws SuspendExecution {
        try {
            return store.put(new byte[0], null);
        } catch (co.paralleluniverse.galaxy.TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void migrate(Object id, Actor actor) throws SuspendExecution {
        final long _id = (Long) id;
        try {
            store.set(_id, new KryoSerializer().write(actor), null);
            store.release(_id);
        } catch (co.paralleluniverse.galaxy.TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Actor hire(Object id) throws SuspendExecution {
        try {
            byte[] buf = store.getx((Long) id, null);
            return (Actor)new KryoSerializer().read(buf);
        } catch (co.paralleluniverse.galaxy.TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
